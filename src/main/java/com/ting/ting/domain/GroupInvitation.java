package com.ting.ting.domain;

import lombok.Getter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Table(name = "\"group_invitation\"")
@FilterDef(name = "validGroupInvitationFilter", parameters = @ParamDef(name = "now", type = "java.time.LocalDateTime"))
@Filter(name = "validGroupInvitationFilter", condition = "expired_at > :now")
@Entity
public class GroupInvitation extends AuditingFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @JoinColumn(name = "group_member_id")
    @OneToOne(optional = false, cascade = CascadeType.PERSIST)
    private GroupMember groupMember;

    @NotNull
    @Column(name = "invitation_code")
    private String invitationCode;

    @NotNull
    @Column(name = "qr_image_url")
    private String qrImageUrl;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    protected GroupInvitation() {}

    private GroupInvitation(GroupMember groupMember, String invitationCode, String qrImageUrl, LocalDateTime expiredAt) {
        this.groupMember = groupMember;
        this.invitationCode = invitationCode;
        this.qrImageUrl = qrImageUrl;
        this.expiredAt = expiredAt;
    }

    public static GroupInvitation of(GroupMember groupMember, String invitationCode, String qrImageUrl, LocalDateTime expiredAt) {
        return new GroupInvitation(groupMember, invitationCode, qrImageUrl, expiredAt);
    }
}
