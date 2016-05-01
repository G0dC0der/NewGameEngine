package pojahn.game.essentials;

public interface ExpiringObject {

    boolean hasExpired();

    void tick();

    static ExpiringObject framesBasedExpiration(int frames) {
        return new ExpiringObject() {
            int counter;
            @Override
            public boolean hasExpired() {
                return counter > frames;
            }

            @Override
            public void tick() {
                counter++;
            }
        };
    }

    static ExpiringObject timeBasedExpiration(long millis) {
        return new ExpiringObject() {
            long expTime = System.currentTimeMillis() + millis;
            @Override
            public boolean hasExpired() {
                return expTime > System.currentTimeMillis();
            }

            @Override
            public void tick() {

            }
        };
    }
}
