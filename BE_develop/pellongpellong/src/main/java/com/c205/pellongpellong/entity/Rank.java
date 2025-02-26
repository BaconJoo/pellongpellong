package com.c205.pellongpellong.entity;

import com.c205.pellongpellong.dto.RankDTO;
import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Table(name = "`Rank`")
public class Rank {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rankId", updatable = false)
    private Long rankId;

    @OneToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "memberId")
    private Member member;

    @Column(name = "sumExp", nullable = false)
    private int sumExp;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Builder
    public Rank(Member member, int sumExp, String nickname) {
        this.member = member;
        this.sumExp = sumExp;
        this.nickname = nickname;
    }

    public RankDTO of(Rank rank) {
        return RankDTO.builder()
                .rankId(rank.getRankId())
                .memberId(rank.getMember().getMemberId())
                .sumExp(rank.getSumExp())
                .nickname(rank.getNickname())
                .build();
    }
}
