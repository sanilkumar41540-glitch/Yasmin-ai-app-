package com.yasmin.ai

import android.content.Context
import android.content.Intent
import android.net.Uri

sealed class ToolRequest(val description: String) {
    class YoutubeSearch(val query: String) : ToolRequest("YouTube par \"$query\" search karna")
    class OpenApp(val name: String) : ToolRequest("\"$name\" app kholna")
}

object ToolRouter {

    /** Returns a tool request if the text looks like a command, else null (-> normal AI chat). */
    fun parse(text: String): ToolRequest? {
        val t = text.trim()
        val lower = t.lowercase()
        return when {
            lower.startsWith("youtube ") -> ToolRequest.YoutubeSearch(t.substring(8).trim())
            lower.startsWith("open ") -> ToolRequest.OpenApp(t.substring(5).trim())
            lower.startsWith("kholo ") -> ToolRequest.OpenApp(t.substring(6).trim())
            else -> null
        }
    }

    /** Call only after the user pressed Allow. */
    fun execute(context: Context, req: ToolRequest): String = when (req) {
        is ToolRequest.YoutubeSearch -> {
            val uri = Uri.parse("https://www.youtube.com/results?search_query=" + Uri.encode(req.query))
            context.startActivity(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            "YouTube khol diya."
        }
        is ToolRequest.OpenApp -> {
            val pm = context.packageManager
            val launch = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            val match = pm.queryIntentActivities(launch, 0)
                .firstOrNull { it.loadLabel(pm).toString().equals(req.name, ignoreCase = true) }
            val intent = match?.let { pm.getLaunchIntentForPackage(it.activityInfo.packageName) }
            if (intent != null) {
                context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                "${req.name} khul gaya."
            } else "\"${req.name}\" naam ka app nahi mila."
        }
    }
}
