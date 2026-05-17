package com.example.englishwordsapp.model;

import com.example.englishwordsapp.security.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "word_set_forks",
        uniqueConstraints = @UniqueConstraint(columnNames = {"original_set_id", "forked_set_id"}))
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class WordSetFork {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_set_id", nullable = false)
    @ToString.Exclude
    private WordSet originalSet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "forked_set_id", nullable = false)
    @ToString.Exclude
    private WordSet forkedSet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    @Column(name = "forked_at", nullable = false)
    private LocalDateTime forkedAt;

    @PrePersist
    protected void onCreate() {
        if (forkedAt == null) {
            forkedAt = LocalDateTime.now();
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
        WordSetFork that = (WordSetFork) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy proxy
                ? proxy.getHibernateLazyInitializer().getPersistentClass().hashCode()
                : getClass().hashCode();
    }
}