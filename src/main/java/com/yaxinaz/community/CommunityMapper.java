package com.yaxinaz.community;

import com.yaxinaz.community.dto.CommunityMemberResponse;
import com.yaxinaz.community.dto.CommunityResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommunityMapper {

    @Mapping(target = "memberCount", ignore = true)
    CommunityResponse toResponse(Community community);

    @Mapping(target = "membershipId", source = "id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "avatarUrl", source = "user.avatarUrl")
    CommunityMemberResponse toMemberResponse(CommunityMembership membership);
}
