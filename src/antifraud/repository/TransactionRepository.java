package antifraud.repository;

import antifraud.entity.Transaction;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionRepository extends CrudRepository<Transaction, Long> {


    @Query("select t from Transaction t where t.number = :number order by t.date DESC")
    List<Transaction> findByNumberOrderByDateDesc(@Param("number") String number);
    Transaction save(Transaction entity);
}