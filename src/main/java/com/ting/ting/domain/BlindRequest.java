package com.ting.ting.domain;

import com.ting.ting.domain.constant.RequestStatus;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Setter
@Getter
@Table(name = "blind_request")
@Entity
public class BlindRequest {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id")
    private User fromUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_id")
    private User toUser;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status;
}
