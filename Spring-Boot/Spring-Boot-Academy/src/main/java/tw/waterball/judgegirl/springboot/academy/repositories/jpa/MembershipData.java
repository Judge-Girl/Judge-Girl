package tw.waterball.judgegirl.springboot.academy.repositories.jpa;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Table(name = "membership")
@Entity(name = "membership")
@Data
@NoArgsConstructor
public class MembershipData {

    @ManyToOne
    @JoinColumn(name = "group_id", referencedColumnName = "id")
    @MapsId("group_id")
    private GroupData group;

    @EmbeddedId
    private Id id;

    public MembershipData(GroupData group, int memberId) {
        this.group = group;
        this.id = new Id(group.getId(), memberId);
    }

    public int getMemberId() {
        return id.getMemberId();
    }

    public int getGroupId() {
        return id.getGroupId();
    }

    @Embeddable
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Id implements Serializable {
        @Column(name = "group_id")
        private int groupId;
        @Column(name = "member_id")
        private int memberId;
    }
}
