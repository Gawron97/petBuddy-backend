package com.example.petbuddybackend.utils.specification;

import com.example.petbuddybackend.dto.criteriaSearch.ChatRoomSearchCriteria;
import com.example.petbuddybackend.entity.chat.ChatMessage;
import com.example.petbuddybackend.entity.chat.ChatRoom;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import com.example.petbuddybackend.entity.user.Role;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Subquery;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.ZonedDateTime;

import static com.example.petbuddybackend.utils.specification.SpecificationCommons.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ChatSpecificationUtils {

    /**
     * Creates a specification for filtering and sorting ChatMessages based on the given criteria.
     * This includes filtering by personal data and ensuring that only the last message
     * in each chat room is returned, sorted by the createdAt field in descending order.
     *
     * @param filters The search criteria to filter ChatMessages.
     * @param principalRole The role of the principal (client or caretaker) for dynamic filtering.
     * @return A Specification that can be used for querying ChatMessage entities.
     */
    public static Specification<ChatMessage> filtersToSpecificationSorted(ChatRoomSearchCriteria filters, String principalEmail, Role principalRole) {
        Specification<ChatMessage> spec = Specification.where(
                (root, query, criteriaBuilder) -> criteriaBuilder.conjunction()
        );

        if(StringUtils.hasText(filters.chatterDataLike())) {
            spec = spec.and(personalDataLike(filters.chatterDataLike(), principalRole));
        }

        return spec.and(chatRoomOfUser(principalEmail, principalRole))
                .and(lastMessageInEachChatRoom())
                .and(orderByCreatedAtDesc());
    }

    private static Specification<ChatMessage> personalDataLike(String personalDataLike, Role principalRole) {
        return SpecificationCommons.personalDataLike(
                personalDataLike,
                root -> {
                    Join<?, ?> chatRoomJoin = root.join(CHAT_ROOM);
                    return principalRole == Role.CLIENT
                            ? chatRoomJoin.join(CARETAKER)
                            : chatRoomJoin.join(CLIENT);
                }
        );
    }

    private static Specification<ChatMessage> chatRoomOfUser(String email, Role role) {
        return (root, query, criteriaBuilder) -> {
            Join<ChatMessage, ChatRoom> chatRoomJoin = root.join(CHAT_ROOM);

            if(role == Role.CARETAKER) {
                Join<ChatRoom, Caretaker> join = chatRoomJoin.join(CARETAKER);
                return criteriaBuilder.equal(join.get(EMAIL), email);
            } else {
                Join<ChatRoom, Client> join = chatRoomJoin.join(CLIENT);
                return criteriaBuilder.equal(join.get(EMAIL), email);
            }
        };
    }

    private static Specification<ChatMessage> lastMessageInEachChatRoom() {
        return (root, query, criteriaBuilder) -> {
            query.distinct(true);

            Subquery<ZonedDateTime> subquery = query.subquery(ZonedDateTime.class);
            var subRoot = subquery.from(ChatMessage.class);

            subquery.select(criteriaBuilder.greatest(subRoot.get(CREATED_AT).as(ZonedDateTime.class)))
                    .where(criteriaBuilder.equal(subRoot.get(CHAT_ROOM), root.get(CHAT_ROOM)));

            return criteriaBuilder.equal(root.get(CREATED_AT), subquery);
        };
    }

    private static Specification<ChatMessage> orderByCreatedAtDesc() {
        return (root, query, criteriaBuilder) -> {
            query.orderBy(criteriaBuilder.desc(root.get(CREATED_AT)));
            return criteriaBuilder.conjunction();
        };
    }
}
