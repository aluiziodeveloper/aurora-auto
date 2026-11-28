package br.com.aurora.claims.legacy;

import java.util.HashMap;
import java.util.Map;

/** The claims in a map, in place of the legacy database. */
public class InMemoryClaimDao implements ClaimDao {

    private final Map<Long, ClaimRecord> claims = new HashMap<>();
    private long lastId = 0;

    @Override
    public ClaimRecord find(long id) {
        return claims.get(id);
    }

    @Override
    public void save(ClaimRecord claim) {
        claims.put(claim.getId(), claim);
    }

    @Override
    public long nextId() {
        lastId = lastId + 1;
        return lastId;
    }
}
