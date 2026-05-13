package com.smartschedule.smartschedule.domain.category.entity;

import com.smartschedule.smartschedule.domain.member.entity.Member;
import com.smartschedule.smartschedule.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "category")
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 20)
    private String color;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Builder
    public Category(String name, String color, Member member) {
        this.name = name;
        this.color = color;
        this.member = member;
    }

    public void update(String name, String color) {
        this.name = name;
        this.color = color;
    }
}
