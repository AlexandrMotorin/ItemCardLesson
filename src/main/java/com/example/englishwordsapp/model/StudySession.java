package com.example.englishwordsapp.model;

import com.example.englishwordsapp.security.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "study_sessions")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class StudySession {

    public enum ExerciseType {
        TEXT_INPUT,
        MULTIPLE_CHOICE,
        MIXED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "exercise_type", nullable = false)
    private ExerciseType exerciseType;

    @Column(name = "word_count", nullable = false)
    private int wordCount;

    @Column(name = "correct_count")
    private int correctCount;

    @Column(name = "current_index", nullable = false)
    private int currentIndex;

    @Column(name = "word_ids_order", columnDefinition = "TEXT")
    private String wordIdsOrder;

    @Column(name = "exercise_types_order", columnDefinition = "TEXT")
    private String exerciseTypesOrder;

    @Column(name = "directions_order", columnDefinition = "TEXT")
    private String directionsOrder;

    @PrePersist
    protected void onCreate() {
        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> objectEffectiveClass = o instanceof HibernateProxy proxy
                ? proxy.getHibernateLazyInitializer().getPersistentClass()
                : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy proxy
                ? proxy.getHibernateLazyInitializer().getPersistentClass()
                : this.getClass();
        if (thisEffectiveClass != objectEffectiveClass) return false;
        StudySession that = (StudySession) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy proxy
                ? proxy.getHibernateLazyInitializer().getPersistentClass().hashCode()
                : getClass().hashCode();
    }
}