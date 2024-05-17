package com.c205.pellongpellong.controller;

import com.c205.pellongpellong.dto.PartyDTO;
import com.c205.pellongpellong.entity.Party;
import com.c205.pellongpellong.repository.MemberRepository;
import com.c205.pellongpellong.service.PartyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@RestController
@RequiredArgsConstructor
public class PartyController {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private final PartyService partyService;
    private final MemberRepository memberRepository;
    private static final Logger logger = LoggerFactory.getLogger(PartyController.class);


    @PostMapping("/party/create/{memberId}")
    public ResponseEntity<?> createParty(@PathVariable Long memberId, @RequestBody Party party) {
        // 해당 멤버가 이미 파티를 생성했는지 확인
        Optional<Party> existingParty = partyService.findPartyByMemberId(memberId);
        if (existingParty.isPresent()) {
            return ResponseEntity.badRequest().body("방을 이미 만드셨습니다.");
        }

        // 파티 생성
        party.setMember(memberRepository.findById(memberId).orElseThrow(() -> new RuntimeException("멤버 값을 찾을 수 없습니다.")));
        Party newParty = partyService.createParty(party);

        //파티id만 반환
        Map<String, Long> result = new HashMap<>();
        result.put("partyId", newParty.getPartyId());
        return ResponseEntity.ok(result);
    }



    @GetMapping("/party")
    public ResponseEntity<List<PartyDTO>> getAllParties() {
        List<PartyDTO> partyDTOs = partyService.listAllPartiesWithDetails();
        return ResponseEntity.ok(partyDTOs);
    }

    @DeleteMapping("/party/delete/{memberId}")
    public ResponseEntity<?> deletePartyByMemberId(@PathVariable Long memberId) {
        Optional<Party> party = partyService.findPartyByMemberId(memberId);
        if (!party.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        partyService.deleteParty(party.get().getPartyId());
        return ResponseEntity.ok().build();
    }


    @GetMapping("/party/{partyId}")
    public void enterUser(@PathVariable Long partyId) {
        String enterMessage =  "대기방에 입장하셨습니다.";
        messagingTemplate.convertAndSend("/topic/party/" + partyId, enterMessage);
        logger.info("Sent message to /topic/party/" + partyId + ": " + enterMessage);
    }

    @MessageMapping(value = "/party/{partyId}/start")
    public void startGame(@Payload StartGameRequest startGameRequest, @PathVariable Long partyId) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "startGame");
        try {
            String jsonMessage = new ObjectMapper().writeValueAsString(message);
            messagingTemplate.convertAndSend("/topic/party/" + partyId, jsonMessage);
            logger.info("Sent message to /topic/party/" + partyId + ": start");
        } catch (JsonProcessingException e) {
            // 로깅 및 오류 처리
            logger.error("JSON 직렬화 오류", e);
        }

    }

    public class StartGameRequest {
        private String partyType;

        public String getPartyType() {
            return partyType;
        }

        public void setPartyType(String partyType) {
            this.partyType = partyType;
        }
    }
}
