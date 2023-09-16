package study.querdsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querdsl.entity.Member;
import study.querdsl.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static study.querdsl.entity.QMember.member;
import static study.querdsl.entity.QTeam.team;

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

    /*
      회원 정렬 순서
      1. 회원 나이 내림 차순(desc)
      2. 회원 이름 올림 차순(asc)
      단 2에서 회원 이름이 없으면 마지막에 출력 (nulls last)
     */
    @Test
    void sort() {
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();

        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member memberNull = result.get(2);

        assertEquals("member5", member5.getUsername());
        assertEquals("member6", member6.getUsername());
        assertNull(memberNull.getUsername());
    }

    @Test
    void paging(){
        List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1) // 0 부터 시작 (zero index)
                .limit(2)  // 최대 2건 조회
                .fetch();

        assertEquals(2, result.size());
    }

    @Test
    void aggregation() {
        List<Tuple> result = queryFactory
                .select(
                        member.count(),     // 회원 수
                        member.age.sum(),   // 나이 합
                        member.age.avg(),   // 나이 평균
                        member.age.max(),   // 나이 최댓값
                        member.age.min()    // 나이 최솟값
                )
                .from(member)
                .fetch();

        Tuple tuple = result.get(0);
        assertEquals(40, tuple.get(member.age.max()));
    }

    /**
     * 팀 이름과 각 팀의 평균 연령을 구해라.
     */
    @Test
    void group() {
        // given when
        List<Tuple> result = queryFactory
                .select(
                        team.name,
                        member.age.avg()
                )
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();
        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        // then
        System.out.println("### teamA = " + teamA);
        System.out.println("### teamB = " + teamB);

        assertEquals("teamA", teamA.get(team.name));
        assertEquals(15L, teamA.get(member.age.avg()));
        assertEquals(35L, teamB.get(member.age.avg()));
    }

    /**
     * 팀 A에 소속된 모든 회원
     */
    @Test
    void join() {
        List<Member> members = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        // then
        assertEquals(2, members.size());

        assertThat(members)
                .extracting("username")
                .containsExactly("member1", "member2");
    }

    /**
     * 세타 조인 (연관 관계 없는 필드로 조인)
     * 회원의 이름이 팀 이름과 같은 회원 조회
     */
    @Test
    void joinTheta() {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));

        List<Member> members = queryFactory
                .select(member)
                .from(member, team)
                .where(team.name.eq(member.username))
                .fetch();

        // then
        assertThat(members)
                .extracting("username")
                .containsExactly("teamA", "teamB");
    }

    /**
     * 회원과 팀을 조인 하면서,
     * 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
     *    * JPQL: SELECT m, t FROM Member m LEFT JOIN m.team t on t.name = 'teamA'
     *    * SQL: SELECT m.*, t.* FROM Member m LEFT JOIN Team t ON m.TEAM_ID=t.id and t.name='teamA'
     */
    @Test
    void joinOnFiltering() {
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team).on(team.name.eq("teamA"))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("### tuple = " + tuple);
        }
        // tuple = [Member(id=3, username=member1, age=10), Team(id=1, name=teamA)]
        // tuple = [Member(id=4, username=member2, age=20), Team(id=1, name=teamA)]
        // tuple = [Member(id=5, username=member3, age=30), null]
        // tuple = [Member(id=6, username=member4, age=40), null]

        assertEquals(4, result.size());
    }

    @Test
    void joinOnNoRelation() {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team).on(member.username.eq(team.name))
                .fetch();

        // then
        for (Tuple tuple : result) {
            System.out.println("### tuple = " + tuple);
        }
        assertEquals(7, result.size());
    }

    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    void fetchJoinNo() {
        em.flush();
        em.clear();

        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        System.out.println("### findMember = " + findMember);
        assert findMember != null;
        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertFalse(loaded, "페치 조인 미적용");
    }

    @Test
    void fetchJoinUse() {
        // 페치 조인 테스트 시에는 결과를 지우지 않으면 제대로 된 결과를 보기 어려움
        em.flush();
        em.clear();

        Member findMember = queryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();

        System.out.println("### findMember = " + findMember);
        assert findMember != null;
        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertTrue(loaded, "페치 조인 적용");
    }

}
