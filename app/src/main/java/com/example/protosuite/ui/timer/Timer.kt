package com.example.protosuite.ui.timer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.protosuite.databinding.TimerBinding
import java.util.*


class Timer : Fragment() {

        companion object {
            fun setAlarm(context: Context, nowSeconds: Long, secondsRemaining: Long){
                val wakeUpTime = (nowSeconds + secondsRemaining) * 1000
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val intent = Intent(context, TimerBroadcastReceiver::class.java)
                intent.putExtra("parentNote", 444)
                val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,wakeUpTime,pendingIntent)
                }else{
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP,wakeUpTime,pendingIntent)
                }
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //binding = DataBindingUtil.inflate(inflater, R.layout.timer, container, false)
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
    }

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

}
