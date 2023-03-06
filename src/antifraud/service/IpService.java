package antifraud.service;

import antifraud.entity.IpSuspicious;
import antifraud.repository.IpSuspiciousRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IpService {
    private final IpSuspiciousRepository ipSuspiciousRepository;

    @Autowired
    public IpService(IpSuspiciousRepository ipSuspiciousRepository) {
        this.ipSuspiciousRepository = ipSuspiciousRepository;
    }

    public IpSuspicious findByIp(String ip) {
        return ipSuspiciousRepository.findByIp(ip);
    }

    public List<IpSuspicious> findAll() {
        return ipSuspiciousRepository.findAll();
    }

    public IpSuspicious saveIp(IpSuspicious ipSuspicious) {
        return ipSuspiciousRepository.save(ipSuspicious);
    }

    public int deleteByIp(String ip) {
        return ipSuspiciousRepository.deleteByIp(ip);
    }

    public boolean isCorrectFormat(String ip) {
        String[] s = ip.split("[.]");
        if (s.length != 4) {
            return false;
        }
        for (String ss : s) {
            if (Integer.parseInt(ss) < 0 || Integer.parseInt(ss) > 255) {
                return false;
            }
        }
        return true;
    }
}
