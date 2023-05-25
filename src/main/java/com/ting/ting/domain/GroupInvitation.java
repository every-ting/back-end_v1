package com.ting.ting.domain;

import lombok.Getter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

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
