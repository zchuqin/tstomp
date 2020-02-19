package stoner.tstomp.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Role {
    private String id;
    private String roleName;
    private Set<Permission> permissions;
}
