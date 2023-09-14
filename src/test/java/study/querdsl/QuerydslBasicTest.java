package study.querdsl;

import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querdsl.entity.Member;
import study.querdsl.entity.QMember;
import study.querdsl.entity.Team;

import javax.persistence.EntityManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static study.querdsl.entity.QMember.member;

/**
 * QuerydslBasicTest
 * <pre>
 * Describe here
 * </pre>
 *
 * @version 1.0,
 */

@SpringBootTest
@Transactional
class QuerydslBasicTest {
    @Autowired
    EntityManager em;

    JPAQueryFactory queryFactory;

    @BeforeEach
    void testEntity() {
        queryFactory = new JPAQueryFactory(em);

        // given
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1" ,10, teamA);
        Member member2 = new Member("member2" ,20, teamA);

        Member member3 = new Member("member3" ,30, teamB);
        Member member4 = new Member("member4" ,40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    void startJPQL() {
        // member1을 찾아라.
        String query = "select m from Member m where m.username = :username";

        Member findMember = em.createQuery(query, Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        assertEquals("member1", findMember.getUsername());
    }

    @Test
    void startQuerydsl() {
        Member findMember = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1"))  // 파라미터 바인
                .fetchOne();

        assert findMember != null;
        assertEquals("member1", findMember.getUsername());
    }

    @Test
    void search() {
        Member findMember = queryFactory
                .selectFrom(member)
                .where(
                        member.username.eq("member1").and(member.age.eq(10))
                )
                .fetchOne();

        assert findMember != null;
        assertEquals("member", findMember.getUsername());
    }

    @Test
    void searchAndParam() {
        Member findMember = queryFactory
                .selectFrom(member)
                .where(
                        member.username.eq("member1"),
                        (member.age.eq(10))
                )
                .fetchOne();

        assert findMember != null;
        assertEquals("member", findMember.getUsername());
    }

    @Test
    void resultFetch() {
        // List 조회
        List<Member> fetch = queryFactory
                .selectFrom(member)
                .fetch();
        System.out.println("### fetch = " + fetch);

        // 단건 조회
        Member fetchOne = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();
        System.out.println("### fetchOne = " + fetchOne);


        // 처음 한건 조회
        Member fetchFirst = queryFactory
                .selectFrom(member)
                .fetchFirst();
        System.out.println("### fetchFirst = " + fetchFirst);

        // 페이징에서 사용 > * 현재 fetchResults (Deprecated) > count 쿼리 직접 조회로 변경 필요
        QueryResults<Member> results = queryFactory
                .selectFrom(member)
                .fetchResults();

        long total = results.getTotal();
        System.out.println("### total = " + total);
        List<Member> content = results.getResults();
        assertNotNull(content);

        // count 쿼리로 변경 > * (Deprecated) > count 쿼리 직접 조회로 변경 필요
        long count = queryFactory
                .selectFrom(member)
                .fetchCount();
        assertEquals(4L, count);

        // 개선 된 count query
        Long count2 = queryFactory
                .select(member.count())
                .from(member)
                .fetchOne();
        assertEquals(4L, count2);
    }
}
