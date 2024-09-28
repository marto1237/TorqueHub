package torquehub.torquehub.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "roles", uniqueConstraints = {
        @UniqueConstraint(columnNames = "name")  // Enforces uniqueness at the database level
})
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // e.g., USER, ORGANIZER, MODERATOR, ADMIN

}
