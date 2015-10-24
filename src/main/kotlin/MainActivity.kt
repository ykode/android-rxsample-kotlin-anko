package com.ykode.research.RxAnkoSample

import android.app.Activity
import android.app.Fragment

import android.os.Bundle
import android.content.Context

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.view.Gravity

import android.widget.EditText
import android.widget.Button

import android.graphics.Color

import android.annotation.TargetApi

import rx.Observable
import rx.Subscription
import rx.subscriptions.CompositeSubscription

import com.jakewharton.rxbinding.widget.*
import org.jetbrains.anko.*
import android.R as AR

import kotlin.text.Regex

var View.enabled: Boolean
  get() = this.isEnabled()
  set(value) = this.setEnabled(value)

class MainActivity : Activity() {
  override fun onCreate(savedInstanceState:Bundle?) {
    super<Activity>.onCreate(savedInstanceState)
    
    val rootId = 1001
    
    frameLayout {
      id = rootId
    }

    if (null == savedInstanceState) {
      fragmentManager.beginTransaction()
        .add(rootId, MainFragment())
        .commit()
    }
  }
}

abstract class ReactiveFragment : Fragment() {
  private var _compoSub = CompositeSubscription()
  private val compoSub: CompositeSubscription
    get() {
      if (_compoSub.isUnsubscribed()) {
        _compoSub = CompositeSubscription()
      }
      return _compoSub
    }

  protected final fun manageSub(s: Subscription) = compoSub.add(s)
  
  override fun onDestroyView() {
    compoSub.unsubscribe()
    super<Fragment>.onDestroyView()
  }
}

internal class MainFragment : ReactiveFragment() {

  lateinit var edtUserName : EditText
  lateinit var edtEmail : EditText
  lateinit var buttonRegister : Button

  override fun onCreateView(inflater: LayoutInflater?,
    container: ViewGroup?, savedInstanceState: Bundle?): View? 
  {
    val margin = ctx.resources.getDimension(R.dimen.activity_all_margin).toInt()
    val largeStyle = AR.style.TextAppearance_Large
    val mediumStyle = AR.style.TextAppearance_Medium

    return with(ctx) {
      verticalLayout {
        padding = margin
        
        textView {
          textResource = R.string.app_name
        }.setTextAppearance(ctx,largeStyle)

        verticalLayout {
          topPadding = dip(40)
          
          textView {
            textResource = R.string.user_name
          }.setTextAppearance(ctx, mediumStyle)
          
          edtUserName = editText().lparams(width=matchParent)
          
          space().lparams(width=matchParent, height=dip(20))

          textView {
            textResource = R.string.email
          }.setTextAppearance(ctx, mediumStyle)

          edtEmail = editText().lparams(width=matchParent)

          buttonRegister = button {
            textResource = R.string.register
            enabled = false
            gravity = Gravity.END
          }.lparams(width=wrapContent, height=wrapContent)
  
        }.lparams(width=matchParent)
      }
    }
  }
  
  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
    val emailPattern = Regex("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"+
                             "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})${'$'}")
                             
  
    val userNameValid = edtUserName.textChanges().map { t -> t.length > 4 }
    val emailValid = edtEmail.textChanges().map { t -> emailPattern in t }

    manageSub(emailValid.distinctUntilChanged()
                  .map{ b -> if (b) Color.WHITE else Color.RED }
                  .subscribe{ color -> edtEmail.setTextColor(color) })

    manageSub(userNameValid.distinctUntilChanged()
                  .map{ b -> if (b) Color.WHITE else Color.RED }
                  .subscribe{ color -> edtUserName.setTextColor(color) })

    val registerEnabled = Observable.combineLatest(userNameValid, emailValid, {a, b -> a && b})

    manageSub(registerEnabled.distinctUntilChanged()
                  .subscribe{ enabled -> buttonRegister.enabled = enabled })
    }
}
