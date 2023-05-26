package com.ting.ting.domain;

import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Table(name = "\"group_invitation\"")
@Entity
public class GroupInvitation extends AuditingFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @JoinColumn(name = "group_member_id")
    @OneToOne(optional = false)
    private GroupMember groupMember;

    @NotNull
    @Column(name = "invitation_code")
    private String invitationCode;

    @NotNull
    @Column(name = "qr_image_url")
    private String qrImageUrl;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime expiredAt;

    protected GroupInvitation() {}

    private GroupInvitation(GroupMember groupMember, String invitationCode, String qrImageUrl) {
        this.groupMember = groupMember;
        this.invitationCode = invitationCode;
        this.qrImageUrl = qrImageUrl;
    }

    public static GroupInvitation of(GroupMember groupMember, String invitationCode, String qrImageUrl) {
        return new GroupInvitation(groupMember, invitationCode, qrImageUrl);
    }
}
