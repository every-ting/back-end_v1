package com.ting.ting.dto.response;

import com.ting.ting.domain.GroupDate;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class GroupDateResponse {

    private Long id;
    private GroupResponse menGroup;
    private GroupResponse womenGroup;

    public static GroupDateResponse from(GroupDate entity) {
        return new GroupDateResponse(
                entity.getId(),
                GroupResponse.from(entity.getMenGroup()),
                GroupResponse.from(entity.getWomenGroup())
        );
    }
}
