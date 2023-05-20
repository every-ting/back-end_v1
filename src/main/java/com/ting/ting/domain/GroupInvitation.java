package com.ting.ting.domain;

import lombok.Getter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Getter
@Table(name = "\"group_invitation\"")
@Entity
public class GroupInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @JoinColumn(name = "group_id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Group group;

    @NotNull
    @Column(name = "invitation_code")
    private String invitationCode;

    @NotNull
    @Column(name = "qr_image_url")
    private String qrImageUrl;

    protected GroupInvitation() {}

    private GroupInvitation(Group group, String invitationCode, String qrImageUrl) {
        this.group = group;
        this.invitationCode = invitationCode;
        this.qrImageUrl = qrImageUrl;
    }

    public static GroupInvitation of(Group group, String invitationCode, String qrImageUrl) {
        return new GroupInvitation(group, invitationCode, qrImageUrl);
    }
}
