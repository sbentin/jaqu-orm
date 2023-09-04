/*
 * Copyright (c) 2020-2024 Shai Bentin & Centimia Inc..
 * All rights reserved.  Unpublished -- rights reserved
 *
 * Use of a copyright notice is precautionary only, and does
 * not imply publication or disclosure.
 *
 * THIS SOFTWARE CONTAINS CONFIDENTIAL INFORMATION AND TRADE
 * SECRETS OF Shai Bentin USE, DISCLOSURE, OR
 * REPRODUCTION IS PROHIBITED WITHOUT THE PRIOR EXPRESS
 * WRITTEN PERMISSION OF Shai Bentin & CENTIMIA, INC.
 */
package com.centimia.orm.jaqu.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Immutability effects two things. 
 * 
 * <ul>
 * <li><b><u>MultiCall caching</u></b>- The multiCallCache holds objects in cache between db calls within the same session. The multi cache
 * makes fetching of data more efficient, because what was fetched before is held in cache and is not fetched again, also it makes sure 
 * that the same object instance is always fetched within the same session and thus, if
 * a change is applied to that object during the session it will reflect in all other objects that hold a relation to it.<br>
 * i.e if we fetch an entity 'A' that holds a relationship with entity 'C' and in another call in the same session we fetch entity 'B' 
 * which also holds a relationship to the same 'C' we will get the same 'C' instance.<br>
 * Sometimes this creates issues and jaQu will throw an exception stating it found two instances of the same entity in the session. 
 * While this is something that usually the developer should take care of, there are cases when this behavior should not be applied 
 * because the related entity is actually immutable and can not change and thus having different instances does not matter. Immutable
 * annotation should be put on such entites to tell jaQu just that and allow multi instances</li>
 * <li><b><u>NoUpdateOnsave</u></b>- Using this annotation you can also achieve the {@link NoUpdateOnSave} behavior, i.e when an entity that holds
 * a relationship to an Immutable annotated entity is updated, the related Immutable entity will not be updated.</li>
 * </ul>
 * 
 * @author shai
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface Immutable {

}
