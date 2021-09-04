import React from 'react';
import clsx from 'clsx';
import styles from './HomepageFeatures.module.css';

const FeatureList = [
  {
    title: 'Easy to Use',
    Png: require('../../static/img/clapperboard-200x200.png').default,
    description: (
      <>
        Whatsub is an easy to use CLI tool for subtitles which can
        convert charset, convert subtitle format and sync.
      </>
    ),
  },
  {
    title: 'Convert SMI to SRT / SRT to SMI',
    Png: require('../../static/img/file-200x200.png').default,
    description: (
      <>
        Convert <code>SAMI</code> (<code>SMI</code>) subtitles to <code>SRT</code>
        and <code>SRT</code> to <code>SMI</code>.
      </>
    ),
  },
  {
    title: 'Sync',
    Png: require('../../static/img/time-200x200.png').default,
    description: (
      <>
        Are your subtitles out of sync? Don't worry! Whatsub can sync it!
      </>
    ),
  },
];

function Feature({Png, title, description}) {
  return (
    <div className={clsx('col col--4')}>
      <div className="text--center">
        <img src={Png} className={styles.featureSng} alt={title} />
      </div>
      <div className="text--center padding-horiz--md">
        <h3>{title}</h3>
        <p>{description}</p>
      </div>
    </div>
  );
}

export default function HomepageFeatures() {
  return (
    <section className={styles.features}>
      <div className="container">
        <div className="row">
          {FeatureList.map((props, idx) => (
            <Feature key={idx} {...props} />
          ))}
        </div>
      </div>
    </section>
  );
}
