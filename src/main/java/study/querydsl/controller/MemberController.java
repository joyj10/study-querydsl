package study.querydsl.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.repository.MemberJpaRepository;
import study.querydsl.repository.MemberRepository;

import java.util.List;

/**
 * MemberController
 * <pre>
 * Describe here
 * </pre>
 *
 * @version 1.0,
 */

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberJpaRepository memberJpaRepository;
    private final MemberRepository memberRepository;

    /**
     * 페이징 처리 없는 회원 검색 API
     * @param condition
     * @return
     */
    @GetMapping("/v1/members")
    public List<MemberTeamDto> searchMemberV1(MemberSearchCondition condition) {
        return memberJpaRepository.search(condition);
    }

    /**
     * 단순 쿼리 페이징 가능한 검색 API
     * (fetchResults() select query, count query 함께 나가나 복잡한 쿼리의 경우 count 정상 적이지 않아 Deprecated 처리)
     * @param condition
     * @param pageable
     * @return
     */
    @GetMapping("/v2/members")
    public Page<MemberTeamDto> searchMemberV2(MemberSearchCondition condition, Pageable pageable) {
        return memberRepository.searchPageSimple(condition, pageable);
    }

    /**
     * 최종 단계 페이징 및 검색 API
     * count query, select query 별도 호출
     * @param condition
     * @param pageable
     * @return
     */
    @GetMapping("/v3/members")
    public Page<MemberTeamDto> searchMemberV3(MemberSearchCondition condition, Pageable pageable) {
        return memberRepository.searchPageComplex(condition, pageable);
    }
}
