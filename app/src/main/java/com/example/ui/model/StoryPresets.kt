package com.example.ui.model

data class StoryPreset(
    val title: String,
    val description: String,
    val script: String,
    val recommendedEmotion: String,
    val recommendedStyle: String,
    val language: String
)

object StoryPresets {
    val presets = listOf(
        StoryPreset(
            title = "Dust Bowl: Black Sunday 1935",
            description = "Historical Documentary",
            script = "In the spring of 1935, a wall of black dust towering two miles high rolled across the Great Plains, turning noon into midnight. For ten agonizing years, the rain refused to fall. An entire generation of homesteaders watched their topsoil take flight on the prairie winds, carrying the dreams of pioneer families into the Atlantic. Yet through the grit, the drought, and the silence of abandoned farms, those who stayed behind bore witness to the enduring strength of the human spirit.",
            recommendedEmotion = "Serious",
            recommendedStyle = "Documentary Narrator",
            language = "English (US)"
        ),
        StoryPreset(
            title = "Echoes of Gettysburg: July 1863",
            description = "War & History Chronicle",
            script = "By the summer of 1863, the American Civil War had ceased to be an adventure. On the quiet orchard ridges of Gettysburg, Pennsylvania, two exhausted armies converged under a merciless July sun. Across three harrowing days, over fifty thousand sons, brothers, and fathers fell among the wheat fields. What remained in the twilight was a wounded nation, sanctified by sacrifice and forever bound to the promise of freedom.",
            recommendedEmotion = "Serious",
            recommendedStyle = "Documentary Narrator",
            language = "English (US)"
        ),
        StoryPreset(
            title = "The Last Campfire",
            description = "Western Frontier Tale",
            script = "The wind across the canyon had a bite to it that evening, whispering secrets only the coyotes understood. Old Jacob pulled his wool coat tight, nudging the embers with the toe of his worn leather boot. Fifty years ago, he’d ridden through these very hills when the grass was tall as a yearling and the frontier stretched out wild and unbroken. Time takes the sharp edge off a man, but the mountains... the mountains never forget.",
            recommendedEmotion = "Calm",
            recommendedStyle = "Frontier Storyteller",
            language = "English (US)"
        ),
        StoryPreset(
            title = "Gold Rush of 1849",
            description = "Historical Memoir",
            script = "We struck Sutter's Creek in the teeth of November. Ice clung to the rocker boxes, and every pan of river silt felt like holding frozen needles. But when that first yellow vein gleamed under the grey California sky, hunger vanished. Men traded their boots, their Bibles, and their common sense for an inch of muddy riverbed. Few came back rich, but none came back the same.",
            recommendedEmotion = "Serious",
            recommendedStyle = "Weathered Cowboy",
            language = "English (US)"
        ),
        StoryPreset(
            title = "Grandfather's Pocket Watch",
            description = "Nostalgic Drama",
            script = "My granddaddy handed me that brass pocket watch the summer before the great harvest. Its crystal was scuffed from thirty years in denim pockets, ticking slow and steady like a stubborn heart. 'Son,' he said, his voice deep as a dry creekbed, 'time don't rush for no king, and it sure won't wait on you. Make every chime count.'",
            recommendedEmotion = "Emotional",
            recommendedStyle = "Campfire Patriarch",
            language = "English (US)"
        ),
        StoryPreset(
            title = "Midnight at Deadwood Pass",
            description = "Cinematic Suspense",
            script = "The storm hit the stagecoach just past Deadwood Pass. Lightning tore through the black timber, flashing off iron horseshoes and nervous eyes. Four miles back, the telegraph wire had gone dead. Sheriff Callahan cocked his Winchester in the dark carriage and told the passengers to keep their heads down. Something was standing in the middle of the road.",
            recommendedEmotion = "Dramatic",
            recommendedStyle = "Frontier Storyteller",
            language = "English (US)"
        ),
        StoryPreset(
            title = "Haveli Ka Purana Raaz",
            description = "Hinglish Mystery Tale",
            script = "Yeh baat tab ki hai jab purani haveli ke darwaze kabhi band nahi hote the. Old Thakur saab baithak mein akele hookah peete hue sochte the ki kaise ek raat mein sab kuch badal gaya. Haveli ki unchi deewaron ne bahut toofaan dekhe the, par us sard raat jo hawa chali, usne zamana badal diya.",
            recommendedEmotion = "Serious",
            recommendedStyle = "Historical Scholar",
            language = "Hinglish"
        )
    )
}
