package br.com.aurora.claims.legacy;

/** Storage of the claims. In the legacy service it was a JDBC class; module 05 brings the database back. */
public interface ClaimDao {

    ClaimRecord find(long id);

    void save(ClaimRecord claim);

    long nextId();
}
