package com.pestdetection.config;

import com.pestdetection.entity.Alert;
import com.pestdetection.entity.PestDetection;
import com.pestdetection.entity.SensorReading;
import com.pestdetection.entity.SmartTrap;
import com.pestdetection.entity.User;
import com.pestdetection.repository.AlertRepository;
import com.pestdetection.repository.DetectionRepository;
import com.pestdetection.repository.SensorRepository;
import com.pestdetection.repository.TrapRepository;
import com.pestdetection.repository.UserRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * On first start (empty database) creates two accounts and a sample farm so the frontend has something to show.
 * Turn off with SEED_ENABLED=false. Change the default passwords before exposing the API.
 */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private static final String[] PESTS = {"Aphid", "Whitefly", "Thrips", "Fall armyworm", "Fruit fly", "Spider mite"};

    private final UserRepository users;
    private final TrapRepository traps;
    private final DetectionRepository detections;
    private final SensorRepository sensors;
    private final AlertRepository alerts;
    private final PasswordEncoder encoder;
    private final String adminPassword;
    private final String userPassword;

    public DataSeeder(UserRepository users, TrapRepository traps, DetectionRepository detections,
                      SensorRepository sensors, AlertRepository alerts, PasswordEncoder encoder,
                      @Value("${app.seed.admin-password}") String adminPassword,
                      @Value("${app.seed.user-password}") String userPassword) {
        this.users = users;
        this.traps = traps;
        this.detections = detections;
        this.sensors = sensors;
        this.alerts = alerts;
        this.encoder = encoder;
        this.adminPassword = adminPassword;
        this.userPassword = userPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (users.count() == 0) {
            users.save(user("admin", "admin@greenfield.example", "ADMIN", adminPassword));
            users.save(user("priya", "priya@greenfield.example", "USER", userPassword));
            log.warn("Seeded demo accounts 'admin' and 'priya'. Change their passwords before production use.");
        }
        if (traps.count() == 0) {
            seedFarm();
            log.info("Seeded sample traps, detections, sensor readings and alerts.");
        }
    }

    private User user(String name, String email, String role, String rawPassword) {
        User u = new User();
        u.setUsername(name);
        u.setEmail(email);
        u.setRole(role);
        u.setPassword(encoder.encode(rawPassword));
        return u;
    }

    private SmartTrap trap(String name, String location, String status, int battery, String deviceId) {
        SmartTrap t = new SmartTrap();
        t.setName(name);
        t.setLocation(location);
        t.setStatus(status);
        t.setBattery(battery);
        t.setDeviceId(deviceId);
        return traps.save(t);
    }

    private void seedFarm() {
        Instant now = Instant.now();
        Random rnd = new Random(11);

        List<SmartTrap> farm = new ArrayList<>();
        String[] favourite = {"Aphid", "Whitefly", "Thrips", "Fall armyworm", "Fruit fly", "Spider mite"};
        farm.add(trap("Polyhouse 1 North", "Block A, tomato", "ACTIVE", 86, "ST-1001"));
        farm.add(trap("Polyhouse 2 South", "Block A, capsicum", "ACTIVE", 72, "ST-1002"));
        farm.add(trap("Chilli plot East", "Block C, open field", "ACTIVE", 54, "ST-1003"));
        farm.add(trap("Cotton block 3", "Block D, open field", "ACTIVE", 91, "ST-1004"));
        farm.add(trap("Mango orchard row 4", "Orchard", "MAINTENANCE", 18, "ST-1005"));
        farm.add(trap("Seedling nursery", "Shade house", "INACTIVE", 0, "ST-1006"));

        // Two weeks of detections, concentrated on the first few traps.
        List<PestDetection> found = new ArrayList<>();
        for (int i = 0; i < 130; i++) {
            int idx = (int) Math.floor(rnd.nextDouble() * rnd.nextDouble() * 5);
            SmartTrap t = farm.get(idx);
            boolean orchard = idx == 4;
            double days = orchard ? 5 + rnd.nextDouble() * 9 : Math.pow(rnd.nextDouble(), 1.7) * 14;
            PestDetection d = new PestDetection();
            d.setTrap(t);
            d.setPestType(rnd.nextDouble() < 0.72 ? favourite[idx] : PESTS[rnd.nextInt(PESTS.length)]);
            d.setQuantity(1 + rnd.nextInt(5));
            d.setConfidence(Math.round((0.72 + rnd.nextDouble() * 0.27) * 100.0) / 100.0);
            d.setDetectedAt(now.minus(Duration.ofMinutes((long) (days * 24 * 60))));
            found.add(d);
        }
        detections.saveAll(found);

        // 24 hourly readings for each of the five working traps, following a daily temperature cycle.
        List<SensorReading> readings = new ArrayList<>();
        for (int ti = 0; ti < 5; ti++) {
            for (int k = 0; k < 24; k++) {
                Instant at = now.minus(Duration.ofHours(23 - k)).minusSeconds(rnd.nextInt(600));
                int hour = at.atZone(java.time.ZoneId.systemDefault()).getHour();
                double cycle = Math.sin((hour - 9) / 24.0 * 2 * Math.PI);
                double temp = 27 + 5 * cycle + (rnd.nextDouble() - 0.5) * 1.6 + ti * 0.3;
                double hum = 70 - 14 * cycle + (rnd.nextDouble() - 0.5) * 4 + (ti == 0 ? 6 : 0);
                SensorReading r = new SensorReading();
                r.setTrap(farm.get(ti));
                r.setTemperature(Math.round(temp * 10.0) / 10.0);
                r.setHumidity(Math.round(Math.min(97, hum) * 10.0) / 10.0);
                r.setRecordedAt(at);
                readings.add(r);
            }
        }
        sensors.saveAll(readings);

        alerts.save(alert(farm.get(0), "HIGH", "Aphid surge: 14 caught in 24 h at Polyhouse 1 North",
                false, false, now.minus(Duration.ofMinutes(130)), "pest|" + farm.get(0).getId() + "|Aphid"));
        alerts.save(alert(farm.get(1), "MEDIUM", "Whitefly count rising at Polyhouse 2 South",
                false, false, now.minus(Duration.ofHours(7)), "pest|" + farm.get(1).getId() + "|Whitefly"));
        alerts.save(alert(farm.get(2), "MEDIUM", "Humidity at 89%. Conditions favour fungal pests at Chilli plot East",
                true, false, now.minus(Duration.ofHours(19)), "hum|" + farm.get(2).getId()));
        alerts.save(alert(farm.get(4), "LOW", "Battery at 18% on Mango orchard row 4. Swap before next round",
                true, false, now.minus(Duration.ofHours(38)), "bat|" + farm.get(4).getId()));
        alerts.save(alert(farm.get(3), "HIGH", "Fall armyworm surge: 11 caught in 24 h at Cotton block 3",
                true, true, now.minus(Duration.ofDays(4)), "pest|" + farm.get(3).getId() + "|Fall armyworm"));
    }

    private Alert alert(SmartTrap trap, String severity, String message, boolean read, boolean resolved,
                        Instant createdAt, String key) {
        Alert a = new Alert();
        a.setTrap(trap);
        a.setSeverity(severity);
        a.setMessage(message);
        a.setRead(read);
        a.setResolved(resolved);
        a.setCreatedAt(createdAt);
        a.setAlertKey(key);
        return a;
    }
}
