package study.querdsl.dto;

import lombok.Data;

/**
 * MemberSearchCondition
 * <pre>
 * Describe here
 * </pre>
 *
 * @version 1.0,
 */

@Data
public class MemberSearchCondition {
    // 회워명, 팀졍, 나이(ageGoe, ageLoe)
    private String username;
    private String teamName;
    private Integer ageGoe;
    private Integer ageLoe;
}
