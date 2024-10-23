package torquehub.torquehub.domain.model.jpa_models;

import jakarta.persistence.*;
import lombok.*;
import torquehub.torquehub.domain.model.plain_models.BaseRole;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "roles", uniqueConstraints = {
        @UniqueConstraint(columnNames = "name")  // Enforces uniqueness at the database level
})
public class JpaRole extends BaseRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // e.g., USER, ORGANIZER, MODERATOR, ADMIN

}
