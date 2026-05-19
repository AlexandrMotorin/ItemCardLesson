package com.example.englishwordsapp.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "study_results")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class StudyResult {

    public enum ExerciseType {
        TEXT_INPUT,
        MULTIPLE_CHOICE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_session_id", nullable = false)
    @ToString.Exclude
    private StudySession studySession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_card_id", nullable = false)
    @ToString.Exclude
    private WordCard wordCard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_set_id")
    @ToString.Exclude
    private WordSet wordSet;

    @Column(name = "is_correct", nullable = false)
    private boolean isCorrect;

    @Enumerated(EnumType.STRING)
    @Column(name = "exercise_type", nullable = false)
    private ExerciseType exerciseType;

    @Column(name = "answered_at", nullable = false)
    private LocalDateTime answeredAt;

    @Column(name = "user_answer", length = 500)
    private String userAnswer;

    @Column(name = "direction", length = 20)
    private String direction;

    @PrePersist
    protected void onCreate() {
        if (answeredAt == null) {
            answeredAt = LocalDateTime.now();
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
        StudyResult that = (StudyResult) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy proxy
                ? proxy.getHibernateLazyInitializer().getPersistentClass().hashCode()
                : getClass().hashCode();
    }
}