package ch.ethz.infk.dspa.recommendations.dto;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PersonSimilarity {

	private Long person1Id;
	private Long person2Id;
	private Double similarity;
	private boolean person1OnlyStatic;

	private Map<String, Integer> categoryMap1;
	private Map<String, Integer> categoryMap2;

	public PersonSimilarity() {

	}

	public PersonSimilarity(Long p1, Long p2) {
		this.person1Id = p1;
		this.person2Id = p2;
	}

	public Long person1Id() {
		return person1Id;
	}

	public Long person2Id() {
		return person2Id;
	}

	public Double similarity() {
		return similarity;
	}

	public boolean person1OnlyStatic() {
		return person1OnlyStatic;
	}

	public Map<String, Integer> getCategoryMap1() {
		return categoryMap1;
	}

	public void setCategoryMap1(Map<String, Integer> categoryMap1) {
		this.categoryMap1 = categoryMap1;
	}

	public Map<String, Integer> getCategoryMap2() {
		return categoryMap2;
	}

	public void setCategoryMap2(Map<String, Integer> categoryMap2) {
		this.categoryMap2 = categoryMap2;
	}

	public PersonSimilarity withPerson1Id(Long id) {
		this.person1Id = id;
		return this;
	}

	public PersonSimilarity withPerson2Id(Long id) {
		this.person2Id = id;
		return this;
	}

	public PersonSimilarity withSimilarity(Double similarity) {
		this.similarity = similarity;
		return this;
	}

	public PersonSimilarity withPerson1OnlyStatic(boolean onlyStatic) {
		this.person1OnlyStatic = onlyStatic;
		return this;
	}

	@Override
	public String toString() {
		return "PersonSimilarity [person1Id=" + person1Id + ", person2Id=" + person2Id + ", similarity=" + similarity
				+ ", person1OnlyStatic=" + person1OnlyStatic + ", categoryMap1=" + categoryMap1 + ", categoryMap2="
				+ categoryMap2 + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((person1Id == null) ? 0 : person1Id.hashCode());
		result = prime * result + (person1OnlyStatic ? 1231 : 1237);
		result = prime * result + ((person2Id == null) ? 0 : person2Id.hashCode());
		result = prime * result + ((similarity == null) ? 0 : similarity.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		PersonSimilarity other = (PersonSimilarity) obj;
		if (person1Id == null) {
			if (other.person1Id != null) return false;
		} else if (!person1Id.equals(other.person1Id)) return false;
		if (person1OnlyStatic != other.person1OnlyStatic) return false;
		if (person2Id == null) {
			if (other.person2Id != null) return false;
		} else if (!person2Id.equals(other.person2Id)) return false;
		if (similarity == null) {
			if (other.similarity != null) return false;
		} else if (!similarity.equals(other.similarity)) return false;
		return true;
	}

	public static PersonSimilarity dotProduct(PersonActivity activity1, PersonActivity activity2) {
		Map<String, Integer> firstMap = activity1.getCategoryMap();
		Map<String, Integer> secondMap = activity2.getCategoryMap();

		Set<String> keys = new HashSet<>();
		keys.addAll(firstMap.keySet());
		keys.addAll(secondMap.keySet());

		double sum = 0;

		for (String key : keys) {
			sum += firstMap.getOrDefault(key, 0) * secondMap.getOrDefault(key, 0);
		}

		return new PersonSimilarity()
				.withPerson1Id(activity1.getPersonId())
				.withPerson1OnlyStatic(activity1.onlyStatic())
				.withPerson2Id(activity2.getPersonId())
				.withSimilarity(sum);
	}

}
