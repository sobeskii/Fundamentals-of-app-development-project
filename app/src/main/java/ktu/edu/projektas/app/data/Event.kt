package ktu.edu.projektas.app.data

 class Event (
        var firebaseId: String = "",
        val id: Long = 0,
        val groupId: Int =0,
        val title: String ="",
        val startTime: Long =0,
        val endTime: Long=0,
        val color : Int=0,
        val location : String="",
        val userUUID : String = ""
        )