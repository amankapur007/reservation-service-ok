package com.aman.learning.microservice;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.lettuce.core.dynamic.annotation.Param;

@EnableDiscoveryClient
@SpringBootApplication
@EnableBinding(Sink.class)
public class ReservationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReservationServiceApplication.class, args);
	}

}

@Component
class DummyDataCLR implements CommandLineRunner {

	ReservationRepository reservationRepository;

	@Autowired
	public DummyDataCLR(ReservationRepository reservationRepository) {
		this.reservationRepository = reservationRepository;
	}

	@Override
	public void run(String... args) throws Exception {
		Stream.of("Aman", "James", "Susie", "Max", "Erik").forEach(n -> {
			this.reservationRepository.save(new Reservation(n));
		});

		reservationRepository.findAll().forEach(System.out::print);
	}

}

@MessageEndpoint
class MessageReader {

	@Autowired
	ReservationRepository reservationRepository;

	@ServiceActivator(inputChannel = Sink.INPUT)
	public void readTheMessage(String r) {
		this.reservationRepository.save(new Reservation(r));
	}
}

@RefreshScope
@RestController
class MessageController {

	@RequestMapping("/reservationsl")
	List<String> names() {
		return Arrays.asList("Aman", "John");
	}
}

@RepositoryRestResource
interface ReservationRepository extends JpaRepository<Reservation, Long> {

	@RestResource(path = "by-name")
	Collection<Reservation> findByReservationName(@Param("rn") String rn);
}

@Entity
class Reservation {

	@Id
	@GeneratedValue
	private long id;
	private String reservationName;

	public Reservation() {
		// TODO Auto-generated constructor stub
	}

	public Reservation(String reservationName) {
		this.reservationName = reservationName;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getReservationName() {
		return reservationName;
	}

	public void setReservationName(String reservationName) {
		this.reservationName = reservationName;
	}

	@Override
	public String toString() {
		return "Reservation [id=" + id + ", reservationName=" + reservationName + "]";
	}

}
