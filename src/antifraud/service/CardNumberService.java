package antifraud.service;

import antifraud.entity.StolenCard;
import antifraud.repository.StolenCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CardNumberService {
    private final StolenCardRepository stolenCardRepository;

    @Autowired
    public CardNumberService(StolenCardRepository stolenCardRepository) {
        this.stolenCardRepository = stolenCardRepository;
    }


    public StolenCard findByNumber(String number) {
        return stolenCardRepository.findByNumber(number);
    }

    public StolenCard save(StolenCard stolenCard){
        return stolenCardRepository.save(stolenCard);
    }

    public int deleteByNumber(String number) {
        return stolenCardRepository.deleteByNumber(number);
    }


    public List<StolenCard> findAll() {
        return stolenCardRepository.findAll();
    }

    public boolean isCorrectFormat(String number) {

        if(number.length()!=16) return false;

        int nDigits = number.length();

        int nSum = 0;
        boolean isSecond = false;
        for (int i = nDigits - 1; i >= 0; i--)
        {

            int d = number.charAt(i) - '0';

            if (isSecond)
                d = d * 2;

            // We add two digits to handle
            // cases that make two digits
            // after doubling
            nSum += d / 10;
            nSum += d % 10;

            isSecond = !isSecond;
        }
        return (nSum % 10 == 0);
    }
}
