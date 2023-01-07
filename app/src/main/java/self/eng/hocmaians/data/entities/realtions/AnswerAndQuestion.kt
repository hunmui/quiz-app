package self.eng.hocmaians.data.entities.realtions

import androidx.room.Embedded
import androidx.room.Relation
import self.eng.hocmaians.data.entities.Question
import self.eng.hocmaians.data.entities.UserAnswer

data class AnswerAndQuestion(
    @Embedded val userAnswer: UserAnswer,
    @Relation(
        parentColumn = "question_id",
        entityColumn = "id"
    )
    val question: Question
)