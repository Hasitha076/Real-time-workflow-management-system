package edu.IIT.task_management.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "collaborators_block")
public class CollaboratorsBlock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int collaboratorsBlockId;
    private int projectId;
    private int workId;

    @ElementCollection
    @CollectionTable(name = "collaborators_block_members", joinColumns = @JoinColumn(name = "collaborators_block_id"))
    @Column(name = "collaborators_block_members_id")
    private List<Integer> memberIds;

    @ElementCollection
    @CollectionTable(name = "collaborators_block_teams", joinColumns = @JoinColumn(name = "collaborators_block_id"))
    @Column(name = "collaborators_block_team_id")
    private List<Integer> teamIds;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
