package com.ting.ting.domain;

import com.ting.ting.domain.constant.Gender;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@NoArgsConstructor
@Setter
@Getter
@Table(name = "blind_date")
@Entity
public class BlindDate extends AuditingFields {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "men_user_id")
    private User menUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "women_user_id")
    private User womenUser;

    private BlindDate(User menUser, User womenUser) {
        this.menUser = menUser;
        this.womenUser = womenUser;
    }

    public static BlindDate from(BlindRequest blindRequest) {
        if (blindRequest.getFromUser().getGender() == Gender.MEN) {
            return new BlindDate(blindRequest.getFromUser(), blindRequest.getToUser());
        }
        return new BlindDate(blindRequest.getToUser(), blindRequest.getFromUser());
    }
}
