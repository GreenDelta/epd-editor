package epd.model.qmeta;

import java.util.ArrayList;
import java.util.List;

import epd.util.Strings;

public class QMetaData {

	public List<QQuestion> questions = new ArrayList<>();

	public QQuestion getQuestion(String id) {
		QQuestion q = questions.stream()
				.filter(e -> Strings.nullOrEqual(e.id, id))
				.findFirst().orElse(null);
		if (q != null)
			return q;
		q = new QQuestion();
		q.id = id;
		questions.add(q);
		return q;
	}

}
