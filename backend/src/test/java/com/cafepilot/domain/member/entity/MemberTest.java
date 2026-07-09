package com.cafepilot.domain.member.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Member 엔티티 단위 테스트")
class MemberTest {

    @Test
    @DisplayName("OWNER 역할로 회원을 생성할 수 있다")
    void createOwner() {
        Member member = Member.createOwner("test@example.com", "encodedPassword", "홍길동");

        assertThat(member.getEmail()).isEqualTo("test@example.com");
        assertThat(member.getName()).isEqualTo("홍길동");
        assertThat(member.getRole()).isEqualTo(Member.Role.OWNER);
        assertThat(member.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("이름을 변경할 수 있다")
    void changeName() {
        Member member = Member.createOwner("test@example.com", "password", "홍길동");

        member.changeName("김철수");

        assertThat(member.getName()).isEqualTo("김철수");
    }

    @Test
    @DisplayName("비밀번호를 변경할 수 있다")
    void changePassword() {
        Member member = Member.createOwner("test@example.com", "oldPassword", "홍길동");

        member.changePassword("newEncodedPassword");

        assertThat(member.getPassword()).isEqualTo("newEncodedPassword");
    }

    @Test
    @DisplayName("소프트 삭제 시 deletedAt이 설정된다")
    void softDelete() {
        Member member = Member.createOwner("test@example.com", "password", "홍길동");

        member.softDelete();

        assertThat(member.isDeleted()).isTrue();
        assertThat(member.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("삭제되지 않은 회원은 isDeleted가 false다")
    void notDeleted() {
        Member member = Member.createOwner("test@example.com", "password", "홍길동");

        assertThat(member.isDeleted()).isFalse();
    }
}
