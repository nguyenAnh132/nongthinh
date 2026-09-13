package com.nongthinh.profile_service.infra.persistence.follow;

import com.nongthinh.profile_service.application.port.out.repository.UserFollowRepository;
import com.nongthinh.profile_service.application.view.*;
import com.nongthinh.profile_service.domain.follow.UserFollow;
import java.sql.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserFollowRepositoryImpl implements UserFollowRepository {
    private final JdbcTemplate jdbc;
    // Only public identity fields are exposed; inactive profiles are excluded from lists and counts.
    private static final String PROFILES = """
            WITH profiles AS NOT MATERIALIZED (
                SELECT user_id, concat_ws(' ', first_name, last_name) display_name,
                       avatar_url, 'FARMER' role FROM farmer_profiles WHERE status='ACTIVE'
                UNION ALL
                SELECT user_id, brand_name, logo_url, 'BRAND' FROM brand_profiles WHERE status='ACTIVE'
            )
            """;
    private static final String FIELDS = """
            SELECT p.*,
              (SELECT count(*) FROM user_follows f JOIN profiles a ON a.user_id=f.follower_user_id
                WHERE f.followed_user_id=p.user_id) follower_count,
              (SELECT count(*) FROM user_follows f JOIN profiles a ON a.user_id=f.followed_user_id
                WHERE f.follower_user_id=p.user_id) following_count,
              EXISTS(SELECT 1 FROM user_follows f WHERE f.follower_user_id=? AND f.followed_user_id=p.user_id) following
            FROM profiles p
            """;

    public Optional<FollowProfileView> profile(UUID userId, UUID viewerId) {
        return jdbc.query(PROFILES + FIELDS + " WHERE p.user_id=?", this::map, viewerId, userId).stream().findFirst();
    }
    public boolean add(UserFollow follow) {
        return jdbc.update("""
                INSERT INTO user_follows(id, follower_user_id, followed_user_id, created_at)
                VALUES (?, ?, ?, ?) ON CONFLICT(follower_user_id, followed_user_id) DO NOTHING
                """, follow.getId(), follow.getFollowerUserId(), follow.getFollowedUserId(),
                Timestamp.from(follow.getCreatedAt())) == 1;
    }
    public void remove(UUID follower, UUID followed) {
        jdbc.update("DELETE FROM user_follows WHERE follower_user_id=? AND followed_user_id=?", follower, followed);
    }
    public FollowPageView list(UUID userId, UUID viewerId, boolean followers, int page, int size) {
        String owner = followers ? "followed_user_id" : "follower_user_id";
        String member = followers ? "follower_user_id" : "followed_user_id";
        var rows = jdbc.query(PROFILES + FIELDS + " JOIN user_follows relation ON relation." + member
                + "=p.user_id WHERE relation." + owner
                + "=? ORDER BY relation.created_at DESC, relation.id DESC LIMIT ? OFFSET ?",
                this::map, viewerId, userId, size + 1, (long) page * size);
        return new FollowPageView(List.copyOf(rows.subList(0, Math.min(size, rows.size()))), page, size, rows.size() > size);
    }
    public Set<UUID> following(UUID viewerId, List<UUID> userIds) {
        if (userIds.isEmpty()) return Set.of();
        var args = new ArrayList<Object>();
        args.add(viewerId);
        args.addAll(userIds);
        return new HashSet<>(jdbc.query(PROFILES + """
                SELECT f.followed_user_id FROM user_follows f JOIN profiles p ON p.user_id=f.followed_user_id
                WHERE f.follower_user_id=? AND f.followed_user_id IN (
                """ + String.join(",", Collections.nCopies(userIds.size(), "?")) + ")",
                (rs, row) -> rs.getObject(1, UUID.class), args.toArray()));
    }
    private FollowProfileView map(ResultSet rs, int row) throws SQLException {
        return new FollowProfileView(rs.getObject("user_id", UUID.class), rs.getString("display_name"),
                rs.getString("avatar_url"), rs.getString("role"), rs.getLong("follower_count"),
                rs.getLong("following_count"), rs.getBoolean("following"));
    }
}
