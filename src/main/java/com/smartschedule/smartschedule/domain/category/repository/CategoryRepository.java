package com.smartschedule.smartschedule.domain.category.repository;

import com.smartschedule.smartschedule.domain.category.entity.Category;
import com.smartschedule.smartschedule.domain.member.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // 특정 회원이 소유한 카테고리 목록 조회
    List<Category> findAllByMemberId(Long memberId);

    // ID와 소유자 ID를 동시에 확인하여 조회
    Optional<Category> findByIdAndMemberId(Long id, Long memberId);

    // 특정 회원이 소유한 특정 카테고리 존재 여부 확인
    boolean existsByIdAndMemberId(Long id, Long memberId);

    // 영속성 컨텍스트를 우회하는 벌크 삭제
    @Modifying
    @Query("DELETE FROM Category c WHERE c.id = :id AND c.member.id = :memberId")
    void deleteBulkByIdAndMemberId(@Param("id") Long id, @Param("memberId") Long memberId);
}
