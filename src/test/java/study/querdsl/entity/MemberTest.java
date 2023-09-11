package study.querdsl.entity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MemberTest
 * <pre>
 * Describe here
 * </pre>
 *
 * @version 1.0,
 */
@SpringBootTest
@Transactional
class MemberTest {
    @Autowired
    EntityManager em;

    @Test
    void testEntity() {
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

        // 초기화
        em.flush();
        em.clear();

        // when
        List<Member> result = em.createQuery("select m from Member m", Member.class).getResultList();

        // then
        for (Member member : result) {
            System.out.println("### member = " + member);
        }

        assertEquals(4, result.size());
    }

}
