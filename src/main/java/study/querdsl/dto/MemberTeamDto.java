package study.querdsl.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

/**
 * MemberTeamDto
 * <pre>
 * Describe here
 * </pre>
 *
 * @version 1.0,
 */
@Data
public class MemberTeamDto {
    private Long memberId;
    private String username;
    private int age;
    private Long teamId;
    private String teamName;

    @QueryProjection
    public MemberTeamDto(Long memberId, String username, int age, Long teamId, String teamName) {
        this.memberId = memberId;
        this.username = username;
        this.age = age;
        this.teamId = teamId;
        this.teamName = teamName;
    }
}
