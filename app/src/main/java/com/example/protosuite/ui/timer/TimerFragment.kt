package com.example.protosuite.ui.timer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.protosuite.ui.notes.NoteViewModel
import com.example.protosuite.ui.values.NotesTheme
import com.example.protosuite.ui.values.yellow50
import dagger.hilt.android.AndroidEntryPoint
import java.util.*


@ExperimentalAnimationApi
@AndroidEntryPoint
class TimerFragment : Fragment() {
/*
        companion object {
            fun setAlarm(context: Context, nowSeconds: Long, secondsRemaining: Long){
                val wakeUpTime = (nowSeconds + secondsRemaining) * 1000
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val intent = Intent(context, TimerBroadcastReceiver::class.java)
                intent.putExtra("parentNote", 444)
                val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,wakeUpTime,pendingIntent)
                PrefUtil.setAlarmEndTime(wakeUpTime, context)
            }

            fun removeAlarm(context: Context){
                val intent = Intent(context, TimerBroadcastReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                alarmManager.cancel(pendingIntent)
                PrefUtil.setAlarmEndTime(0, context)
            }

        val nowSeconds: Long
            get() = Calendar.getInstance().timeInMillis / 1000
    }
    enum class TimerState{
        Stopped,Paused,Running
    }

    private lateinit var timer: CountDownTimer
    private var timerLengthSeconds: Long = 0
    private var timerState = TimerState.Stopped

    private var secondsRemaining: Long = 0

    //private lateinit var binding: TimerBinding
    private var _binding: TimerBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
 */

    /*
    var bundle = bundleOf(
        "triggerTime" to (System.currentTimeMillis() + (secondsRemaining*1000)),
        "parentNote" to 0
    )


    private val pendingTimerReceiver: PendingIntent by lazy {
        intent = Intent(requireActivity().applicationContext, TimerBroadcastReceiver::class.java)
        intent.putExtras(bundle)
        PendingIntent.getBroadcast(requireActivity().applicationContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
    }
 */
    // Lazy Inject ViewModel
    private val myViewModel: NoteViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        /*
        binding = DataBindingUtil.inflate(inflater, R.layout.timer, container, false)
        _binding = TimerBinding.inflate(inflater,container,false)
        val view = binding.root

        binding.fabStart.setOnClickListener {
            if (timerState == TimerState.Stopped) {
                useNewTimerLength()
            }else{
                usePreviousTimerLength()
            }
            startTimer()
            PrefUtil.setTimerState(timerState,requireContext())
            updateButtons()
            setAlarm(requireActivity().applicationContext, nowSeconds, secondsRemaining)
        }
        binding.fabPause.setOnClickListener {
            timer.cancel()
            timerState = TimerState.Paused
            PrefUtil.setTimerState(timerState,requireContext())
            updateButtons()
            removeAlarm(requireContext())
        }
        binding.fabStop.setOnClickListener {
            timer.cancel()
            onTimerFinished()
            PrefUtil.setTimerState(timerState,requireContext())
            removeAlarm(requireContext())
        }

        return view
         */
        return ComposeView(requireContext()).apply {
            setContent {
                //TimerUI()
            }
        }
    }
/*
    override fun onResume() {
        super.onResume()

        initTimer()

        //TODO: NotificationUtil.hideTimerNotification(requireContext())
    }

    override fun onPause() {
        super.onPause()

        if (timerState == TimerState.Running){
            timer.cancel()
            //val wakeUpTime = setAlarm(requireContext(), nowSeconds, secondsRemaining)
            //TODO: NotificationUtil.showTimerRunning(requireContext(), wakeUpTime)
        }
        else if (timerState == TimerState.Paused){
            PrefUtil.setSecondsRemaining(secondsRemaining, requireContext())
            //TODO: NotificationUtil.showTimerPaused(requireContext())
        }

        PrefUtil.setPreviousTimerLengthSeconds(timerLengthSeconds, requireContext())
        //PrefUtil.setSecondsRemaining(secondsRemaining, requireContext())
        //PrefUtil.setTimerState(timerState, requireContext())
    }

    private fun initTimer(){
        timerState = PrefUtil.getTimerState(requireContext())

        when (timerState) {
            TimerState.Running -> {
                usePreviousTimerLength()
                secondsRemaining = ((PrefUtil.getAlarmEndTime(requireContext())/1000) - nowSeconds)
                startTimer()
            }
            TimerState.Paused -> {
                usePreviousTimerLength()
                secondsRemaining = PrefUtil.getSecondsRemaining(requireContext())

            }
            TimerState.Stopped -> {
                //useNewTimerLength()
            }
        }

        updateButtons()
        updateCountdownUI()
    }

    private fun onTimerFinished(){
        timerState = TimerState.Stopped

        //set the length of the timer to be the one set in SettingsActivity
        //if the length was changed when the timer was running
        usePreviousTimerLength()

        binding.progressBar.progress = 0

        PrefUtil.setSecondsRemaining(timerLengthSeconds, requireContext())
        secondsRemaining = timerLengthSeconds

        updateButtons()
        updateCountdownUI()
    }

    private fun startTimer(){
        timerState = TimerState.Running

        timer = object : CountDownTimer(secondsRemaining * 1000, 1000) {
            override fun onFinish() = onTimerFinished()

            override fun onTick(millisUntilFinished: Long) {
                secondsRemaining = millisUntilFinished / 1000
                updateCountdownUI()
            }
        }.start()
    }

    private fun useNewTimerLength(){
        //val lengthInMinutes = PrefUtil.getTimerLength(requireContext())
        //timerLengthSeconds = (lengthInMinutes * 60L)
        if (binding.editTextTime.text.toString() == ""){
            usePreviousTimerLength()
        }else {
            timerLengthSeconds = binding.editTextTime.text.toString().toLong()
        }
        binding.progressBar.max = timerLengthSeconds.toInt()
        if (timerState == TimerState.Stopped){
            secondsRemaining = timerLengthSeconds
        }
    }

    private fun usePreviousTimerLength(){
        //timerLengthSeconds = getAlarmExtra(requireContext(),"TimerLengthMilli")!! / 1000
        timerLengthSeconds = PrefUtil.getPreviousTimerLengthSeconds(requireContext())
        binding.progressBar.max = timerLengthSeconds.toInt()
    }

    private fun updateCountdownUI(){
        val minutesUntilFinished = secondsRemaining/60
        val secondsInMinuteUntilFinished = secondsRemaining - minutesUntilFinished * 60
        val secondsStr = secondsInMinuteUntilFinished.toString()
        val text = "$minutesUntilFinished:${
        if (secondsStr.length == 2){
            secondsStr
        }else{
            "0$secondsStr"
        }
        }"
        binding.timerLength.text = text
        binding.progressBar.progress = (timerLengthSeconds - secondsRemaining).toInt()
    }

    private fun updateButtons(){
        when (timerState){
            TimerState.Running ->{
                binding.fabStart.isEnabled = false
                binding.fabPause.isEnabled = true
                binding.fabStop.isEnabled = true
                binding.editTextTime.isEnabled = false
            }
            TimerState.Stopped ->{
                binding.fabStart.isEnabled = true
                binding.fabPause.isEnabled = false
                binding.fabStop.isEnabled = false
                binding.editTextTime.isEnabled = true
            }
            TimerState.Paused ->{
                binding.fabStart.isEnabled = true
                binding.fabPause.isEnabled = false
                binding.fabStop.isEnabled = true
                binding.editTextTime.isEnabled = true
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        if (timerState == TimerState.Running) {
            timer.cancel()
        }
        _binding = null
    }
 */
}

enum class TimerState {
    Stopped,Paused,Running
}

val nowSeconds: Long
    get() = Calendar.getInstance().timeInMillis / 1000

@ExperimentalAnimationApi
fun setAlarm(context: Context, nowSeconds: Long, secondsRemaining: Long) {
    val wakeUpTime = (nowSeconds + secondsRemaining) * 1000
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, TimerBroadcastReceiver::class.java)
    intent.putExtra("parentNote", 444)
    val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,wakeUpTime,pendingIntent)
    PrefUtil.setAlarmEndTime(wakeUpTime, context)
}

@ExperimentalAnimationApi
fun removeAlarm(context: Context) {
    val intent = Intent(context, TimerBroadcastReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.cancel(pendingIntent)
    PrefUtil.setAlarmEndTime(0, context)
}

// play: run parsed time on clock, pause, stop alarm and leave time on clock (and save time in preferences
// stop: remove alarm and reset time to 0
// on a button press save TimerState to preferences
// on TimerState.Stopped, nothing, Paused, return value to timer, running, create time left value and start countdown timer
@ExperimentalAnimationApi
@Composable
fun TimerUI(myViewModel: NoteViewModel) {
    val timerLength: Long by myViewModel.timerLength.observeAsState(0)
    val timerState: TimerState by myViewModel.timerState.observeAsState(TimerState.Stopped)
    val hour = timerLength.div(60*60)
    val min = timerLength.div(60).mod(60)
    val sec = timerLength.mod(60)
    val formattedTimerLength: String = String.format("%02d:%02d:%02d", hour, min, sec)
    NotesTheme(darkTheme = false) {
        Column(
            modifier = Modifier
                .padding(start = 8.dp, top = 4.dp, end = 8.dp, bottom = 4.dp)
                .fillMaxSize()
                .background(yellow50),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                TimeButtonsAdd(myViewModel)
                //if (TimerState.Running && timer <= 5sec){flashing text}else{regular text}
                if (timerState == TimerState.Running && timerLength <= 5) {
                    FlashingTimerText(formattedTimerLength)
                } else {
                    TimerText(Modifier, formattedTimerLength)
                }
                TimeButtonsSub(myViewModel)
            }
            PlayPauseStop(myViewModel)
        }
    }
}

@ExperimentalAnimationApi
@Preview
@Composable
private fun TimerUITest() {
    //TimerUI()
}

@ExperimentalAnimationApi
@Composable
private fun PlayPauseStop(myViewModel: NoteViewModel) {
    val context = LocalContext.current
    val timerLength: Long by myViewModel.timerLength.observeAsState(0)
    val timerState: TimerState by myViewModel.timerState.observeAsState(
        PrefUtil.getTimerState(
            context
        )
    )
    //var timerState: TimerState by rememberSaveable { mutableStateOf(PrefUtil.getTimerState(context))}
    val icon =
        if (timerState == TimerState.Running) Icons.Default.Pause else Icons.Default.PlayArrow
    val tint by animateColorAsState(
        targetValue = if (timerState == TimerState.Running) Color.Yellow else Color.Green,
        animationSpec = tween(
            durationMillis = 400,
            easing = LinearEasing
        )
    )
    //var expand by remember { mutableStateOf(false)}
    //val stopVisible: Boolean = timerState != TimerState.Stopped
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        FloatingActionButton(
            onClick = {
                if (timerLength != 0L) {
                    if (timerState == TimerState.Running) { // Clicked Pause
                        myViewModel.setTimerState(TimerState.Paused, context)
                        removeAlarm(context)
                        myViewModel.stopTimer()
                    } else {    // Clicked Start
                        if (timerState == TimerState.Stopped) {
                            PrefUtil.setPreviousTimerLengthSeconds(timerLength, context)
                        }
                        myViewModel.setTimerState(TimerState.Running, context)
                        setAlarm(
                            context = context,
                            nowSeconds = nowSeconds,
                            secondsRemaining = timerLength
                        )
                        myViewModel.startTimer(timerLength, context)
                    }
                }
            },
            backgroundColor = tint
        ) {
            Icon(icon, contentDescription = "Start or Pause")
        }
        AnimatedVisibility(
            visible = timerState != TimerState.Stopped,
            enter = slideInHorizontally() + fadeIn(),
            exit = slideOutHorizontally() + fadeOut()
        ) {
            FloatingActionButton(
                onClick = { // Clicked Stop
                    if (timerLength != 0L) {
                        myViewModel.setTimerState(TimerState.Stopped, context)
                        removeAlarm(context)
                        myViewModel.stopTimer()
                        myViewModel.setTimerLength(PrefUtil.getPreviousTimerLengthSeconds(context))
                    }
                          },
                backgroundColor = Color.Red
            ) {
                Icon(Icons.Default.Stop, contentDescription = "Stop")
            }
        }
    }
}

@Composable
fun FlashingTimerText(timerText: String) {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    TimerText(
        modifier = Modifier.alpha(alpha),
        timerText = timerText
    )
}

@Composable
fun TimerText(modifier: Modifier = Modifier, timerText: String) {
    Text(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        style = MaterialTheme.typography.h1,
        text = timerText,
        textAlign = TextAlign.Center
    )
}

@ExperimentalAnimationApi
@Composable
fun TimeButtonsAdd(myViewModel: NoteViewModel) {
    val context = LocalContext.current
    val timerLength: Long by myViewModel.timerLength.observeAsState(initial = 0)
    val timerState: TimerState by myViewModel.timerState.observeAsState(
        PrefUtil.getTimerState(
            context
        )
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        AnimatedVisibility(
            visible = timerState != TimerState.Running,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Button(onClick = {
                myViewModel.setTimerLength(timerLength + 60 * 60)
            }) {
                Text(text = "+")
            }
        }
        AnimatedVisibility(
            visible = timerState != TimerState.Running,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Button(onClick = {
                myViewModel.setTimerLength(timerLength + 60)
            }) {
                Text(text = "+")
            }
        }
        AnimatedVisibility(
            visible = timerState != TimerState.Running,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Button(onClick = {
                myViewModel.setTimerLength(timerLength + 1)
            }) {
                Text(text = "+")
            }
        }
    }
}

@ExperimentalAnimationApi
@Composable
fun TimeButtonsSub(myViewModel: NoteViewModel) {
    val context = LocalContext.current
    val timerLength: Long by myViewModel.timerLength.observeAsState(initial = 0)
    val timerState: TimerState by myViewModel.timerState.observeAsState(
        PrefUtil.getTimerState(
            context
        )
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        AnimatedVisibility(
            visible = timerState != TimerState.Running,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Button(onClick = {
                myViewModel.setTimerLength(timerLength - 60 * 60)
            }) {
                Text(text = "-")
            }
        }
        AnimatedVisibility(
            visible = timerState != TimerState.Running,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Button(onClick = {
                myViewModel.setTimerLength(timerLength - 60)
            }) {
                Text(text = "-")
            }
        }
        AnimatedVisibility(
            visible = timerState != TimerState.Running,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Button(onClick = {
                myViewModel.setTimerLength(timerLength - 1)
            }) {
                Text(text = "-")
            }
        }
    }
}

/*
@Preview
@Composable
fun TimerTextPreview() {
    TimerText(timerText = "00:21")
}

@Preview
@Composable
fun FlashingTimerTextPreview() {
    FlashingTimerText(timerText = "00:00")
}
 */