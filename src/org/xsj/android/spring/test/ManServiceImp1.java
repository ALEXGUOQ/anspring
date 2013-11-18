package org.xsj.android.spring.test;

import org.xsj.android.spring.common.Logx;
import org.xsj.android.spring.core.annotation.Component;
import org.xsj.android.spring.core.db.annotation.Transactional;

@Component("manServiceImp1")
public class ManServiceImp1 implements ManService {
	
	/* (non-Javadoc)
	 * @see org.xsj.android.spring.test.IManService#save()
	 */
	@Override
	@Transactional
	public void save(){
		Logx.i("manservice-save()");
	}
}
