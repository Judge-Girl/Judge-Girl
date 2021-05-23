package tw.waterball.judgegirl.primitives.exam;

import lombok.*;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.commons.utils.ValidationUtils;
import tw.waterball.judgegirl.primitives.Student;

import javax.validation.constraints.NotBlank;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static tw.waterball.judgegirl.commons.utils.StreamUtils.mapToList;

/**
 * @author - wally55077@gmail.com
 */
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Group {

    private Integer id;

    @NotBlank
    private String name;

    private Set<MemberId> memberIds = new HashSet<>();

    public Group(String name) {
        this.name = name;
    }

    public Group(int id, String name) {
        this(name);
        this.id = id;
    }

    public Group(String name, Set<MemberId> memberIds) {
        this.name = name;
        this.memberIds = memberIds;
    }

    public void validate() {
        ValidationUtils.validate(this);
    }

    public void addMember(MemberId memberId) {
        memberIds.add(memberId);
    }

    public void addMembers(Collection<MemberId> memberIds) {
        this.memberIds.addAll(memberIds);
    }

    public void addStudentsAsMembers(List<Student> students) {
        addMembers(mapToList(students, s -> new MemberId(s.getId())));
    }

    public void addStudentsAsMembers(Student... students) {
        addMembers(mapToList(students, s -> new MemberId(s.getId())));
    }

    public void deleteMember(MemberId memberId) {
        this.memberIds.remove(memberId);
    }

    public void deleteMembers(Set<MemberId> memberIds) throws NotFoundException {
        this.memberIds.removeAll(memberIds);
    }
}
