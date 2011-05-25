package org.jw.mmn.core.model.entity;

import org.jw.mmn.commons.model.entity.Phone;

import org.jw.mmn.core.model.entity.Member;
import org.vulpe.model.annotations.db4o.Inheritance;

@Inheritance
@SuppressWarnings("serial")
public class MemberPhone extends Phone {

	private Member member;

	public void setMember(Member member) {
		this.member = member;
	}

	public Member getMember() {
		return member;
	}

}