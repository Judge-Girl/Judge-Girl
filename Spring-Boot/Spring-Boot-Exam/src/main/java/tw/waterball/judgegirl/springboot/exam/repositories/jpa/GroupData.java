package tw.waterball.judgegirl.springboot.exam.repositories.jpa;

import lombok.*;
import tw.waterball.judgegirl.entities.exam.Group;
import tw.waterball.judgegirl.entities.exam.MemberId;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static tw.waterball.judgegirl.commons.utils.StreamUtils.mapToList;

/**
 * @author - wally55077@gmail.com
 * @author - johnny850807@gmail.com (Waterball)
 */
@Table(name = "study_groups" /*escape from keywords*/)
@Setter
@Getter
@Builder
@Entity(name = "study_groups")
@NoArgsConstructor
@AllArgsConstructor
public class GroupData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @OneToMany(mappedBy = "group",
            cascade = {CascadeType.ALL}, fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<MembershipData> memberships = new HashSet<>();

    public GroupData(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public static GroupData toData(Group group) {
        GroupData groupData = new GroupData(group.getId(), group.getName());
        groupData.memberships.addAll(mapToList(group.getMemberIds(),
                memberId -> new MembershipData(groupData, memberId.getId())));
        return groupData;
    }

    public Group toEntity() {
        Group group = new Group(id, name);
        group.addMembers(mapToList(memberships, m -> new MemberId(m.getMemberId())));
        return group;
    }

}
