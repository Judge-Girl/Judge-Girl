package tw.waterball.judgegirl.springboot.academy.repositories.jpa.impl;

import tw.waterball.judgegirl.springboot.academy.repositories.jpa.GroupData;

import java.util.Set;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public interface GetGroupsOwnByMember {
    Set<GroupData> getGroupsOwnedByMember(int memberId);
}
