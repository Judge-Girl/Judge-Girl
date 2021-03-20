package tw.waterball.judgegirl.springboot.student.controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tw.waterball.judgegirl.springboot.student.repositories.jpa.GroupData;
import tw.waterball.judgegirl.springboot.student.repositories.jpa.GroupDataRepository;

/**
 * @author - wally55077@gmail.com
 */
@RestController
@AllArgsConstructor
public class GroupController {

    private final GroupDataRepository repository;


    @PostMapping("/api/groups")
    public GroupData createGroup(@RequestBody GroupCreateRequest request) {
        String name = request.getName();
        if (repository.existsGroupDataByName(name)) {
            throw new DuplicateGroupNameException();
        }
        GroupData groupData = GroupData.builder()
                .name(name).build();
        return repository.saveAndFlush(groupData);
    }

    @ExceptionHandler({DuplicateGroupNameException.class})
    public ResponseEntity<?> badRequestHandler(Exception err) {
        return ResponseEntity.badRequest().body(err.getMessage());
    }

}
