package ru.tushkov.helpdesk.ticket;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;

public final class TicketSpecifications {

    private TicketSpecifications() {}

    public static Specification<Ticket> matching(TicketFilter filter, Instant now) {
        return (root, query, builder) -> {
            if (query != null
                    && !Long.class.equals(query.getResultType())
                    && !long.class.equals(query.getResultType())) {
                root.fetch("category", JoinType.LEFT);
                query.distinct(true);
            }
            List<Predicate> predicates = new ArrayList<>();
            if (filter.status() != null) {
                predicates.add(builder.equal(root.get("status"), filter.status()));
            }
            if (filter.priority() != null) {
                predicates.add(builder.equal(root.get("priority"), filter.priority()));
            }
            if (filter.categoryId() != null) {
                predicates.add(builder.equal(root.get("category").get("id"), filter.categoryId()));
            }
            if (filter.overdue() != null) {
                Predicate open = root.get("status").in(TicketStatus.NEW, TicketStatus.IN_PROGRESS);
                Predicate dueReached = builder.and(
                        builder.isNotNull(root.get("dueAt")), builder.lessThanOrEqualTo(root.get("dueAt"), now));
                Predicate overdue = builder.and(open, dueReached);
                predicates.add(Boolean.TRUE.equals(filter.overdue()) ? overdue : builder.not(overdue));
            }
            if (filter.query() != null && !filter.query().isBlank()) {
                String pattern = "%" + likePattern(filter.query()) + "%";
                predicates.add(builder.or(
                        builder.like(builder.lower(root.get("title")), pattern, '\\'),
                        builder.like(builder.lower(builder.coalesce(root.get("description"), "")), pattern, '\\')));
            }
            if (predicates.isEmpty()) {
                return builder.conjunction();
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static String likePattern(String value) {
        return value.trim()
                .toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
